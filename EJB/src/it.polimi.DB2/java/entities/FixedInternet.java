package entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "fixed_internet", schema = "db2_project")
@DiscriminatorValue("1")
public class FixedInternet extends Service {
    @Column(name = "n_gigabytes")
    private int nGigabytes;
    @Column(name = "fee")
    private double feeGigabytes;

    public int getnGigabytes() {
        return nGigabytes;
    }

    public double getFeeGigabytes() {
        return feeGigabytes;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FixedInternet that = (FixedInternet) o;

        if (nGigabytes != that.nGigabytes) return false;
        return Double.compare(that.feeGigabytes, feeGigabytes) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = nGigabytes;
        temp = Double.doubleToLongBits(feeGigabytes);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
