package entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "optional_product", schema = "db2_project")
public class OptionalProduct implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "monthly_fee")
    private double monthlyFee;
    @ManyToMany(mappedBy = "optionalProductList")
    @JoinTable(name = "order_to_optional_product", joinColumns = @JoinColumn(name = "id_optional_product"), inverseJoinColumns = @JoinColumn(name = "id_order"))
    private List<Order> orderList;
    @ManyToMany(mappedBy = "optionalProductList")
    @JoinTable(name = "service_package_to_optional_product", joinColumns = @JoinColumn(name = "id_optional_product"), inverseJoinColumns = @JoinColumn(name = "id_service_package"))
    private List<ServicePackage> servicePackagesList;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OptionalProduct that = (OptionalProduct) o;

        if (id != that.id) return false;
        if (Double.compare(that.monthlyFee, monthlyFee) != 0) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(monthlyFee);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
