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
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "monthly_fee")
    private double monthlyFee;

    public OptionalProduct() {
    }

    public OptionalProduct(String name, double monthlyFee) {
        this.name = name;
        this.monthlyFee = monthlyFee;
    }

    public long getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public double getMonthlyFee() {
        return monthlyFee;
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
        return Objects.hash(id, name, monthlyFee);
    }
}
