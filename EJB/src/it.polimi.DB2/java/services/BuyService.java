package services;

import entities.*;
import utils.PaymentRevisionBot;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.ws.rs.BadRequestException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Http Session is a perfect storage place where all request from same web client have access
 * to all attributes stored in http session. So you ask for it from the container and keep it in a place (http session)
 * from where it can be referred again by all requests for same session.
 * Therefore, if in the session there's no attribute userQuestionnaire, it means that it's the first time the user is fulfilling
 * the product's questionnaire
 * -> @Stateful EJBs can be also denoted with @SessionScoped annotation.
 * Then the active HTTP session becomes the "EJB client" and maintains the instances.
 */

@Stateful
@StatefulTimeout(value = 10)
public class BuyService implements Serializable {

    @PersistenceContext(unitName = "db2_project", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    /* ---- Object Status ---- */
    private Order order;
    private ServicePackage servicePackage;
    private final Map<OptionalProduct, Boolean> optionalProductBooleanMap = new HashMap<>();


    public void initOrder(Long idService) {
        order = new Order();
        optionalProductBooleanMap.clear();
        ServicePackage servicePackage = em.find(ServicePackage.class, idService);
        if (servicePackage == null) throw new BadRequestException();
        this.servicePackage = servicePackage;
        initOptionalProductBooleanMap();
    }

    //TODO:remove
    public boolean isInitialized() {
        return servicePackage != null;
    }

    public ServicePackage getServicePackage() {
        return servicePackage;
    }

    public void setOffer(int id) {
        /*The only valid flow to call this method is via the CustomizeOrder post method,
        so the user must have already accessed the get method of the same controller -> This bean is already initialized*/
        if (order == null) throw new BadRequestException();
        Offer offer = em.find(Offer.class, id);
        if (offer == null || !offer.isActive()) throw new BadRequestException();
        if (offer.equals(order.getOffer())) return;
        if (!offer.getServicePackage().equals(this.servicePackage)) throw new BadRequestException();
        order.setOffer(offer);
    }

    public Map<OptionalProduct, Boolean> getOptionalProduct() {
        return optionalProductBooleanMap;
    }

    public void setOptionalProducts(String[] opProds) throws IllegalAccessException, NumberFormatException {
        initOptionalProductBooleanMap();
        order.getOptionalProductList().clear();
        for (String opId : opProds) {
            int idOpProd = Integer.parseInt(opId);
            Optional<OptionalProduct> optional = optionalProductBooleanMap.keySet().stream().filter(x -> x.getId() == idOpProd).findAny();
            if (optional.isEmpty()) throw new IllegalAccessException();
            OptionalProduct o = optional.get();
            optionalProductBooleanMap.replace(o, Boolean.TRUE);
            order.getOptionalProductList().add(o);
        }
    }

    public void setStartDate(Date date) {
        order.setActivationDate(date);
    }

    public Order getOrder() {
        if (order != null && order.getOffer() != null) {
            double sum = 0;
            sum = sum + order.getOffer().getMonthlyFee();
            for (Map.Entry<OptionalProduct, Boolean> e : optionalProductBooleanMap.entrySet()) {
                if (e.getValue()) sum = sum + e.getKey().getMonthlyFee();
                order.setTotalMonthlyFee(sum);
            }
        }
        return order;
    }

    //TODO:remove
    public boolean isCorrectFilled(boolean userIsImportant) {
        return order.isCorrectFilled(userIsImportant);
    }

    @Remove
    public boolean executePayment(Customer customer) {
        /*Specification: "When the user presses the BUY button, an order is created",
         * so the creation date is set manually only in this method*/
        if(order.getOffer() == null || order.getActivationDate() == null){
            throw new BadRequestException();
        }

        order.setCustomer(customer);
        order.setCreationDate(new Date());
        //TODO: settare correttamente;
        order.setDeactivationDate(new Date());
        boolean isPaymentValid = PaymentRevisionBot.review();

        if (isPaymentValid) {
            order.setStatus(Order.State.PAID);
        } else {
            order.setStatus(Order.State.PAYMENT_FAILED);
            customer.addOneFailedPayment();
            //It's already an audit customer
            if (customer.isAuditCustomer()) {
                AuditCustomer a = em.find(AuditCustomer.class, customer.getId());
                a.setAmount(order.getTotalMonthlyFee());
                a.setLastRejection(order.getCreationDate());
            } else if (customer.getNumFailedPayments() >= 3) {
                //Here only if it's becoming an Audit Customer
                customer.setAuditCustomer(true);
                AuditCustomer a = new AuditCustomer(customer, order.getTotalMonthlyFee(), order.getCreationDate());
                em.persist(a);
            }
            em.merge(customer);
        }

        em.persist(order);
        return isPaymentValid;
    }

    @Remove
    public void stopProcess() {
    }

    /* ----- PRIVATE METHODS ------ */

    private void initOptionalProductBooleanMap() {
        for (OptionalProduct o : servicePackage.getOptionalProductList()) {
            optionalProductBooleanMap.put(o, Boolean.FALSE);
        }
    }
}