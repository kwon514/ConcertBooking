package proj.concert.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


@Entity
@NamedQueries({
    @NamedQuery(name="Seat.getAny", query="SELECT s FROM Seat s WHERE s.date=?1"),
    @NamedQuery(name="Seat.getBooked", query="SELECT s FROM Seat s WHERE s.date=?1 AND s.isBooked=true"),
    @NamedQuery(name="Seat.getUnbooked", query="SELECT s FROM Seat s WHERE s.date=?1 AND s.isBooked=false")
})
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	private String label;
	private boolean isBooked;
	private LocalDateTime date;
	private BigDecimal cost;

	public Seat() {
	}

	public Seat(Long id, String label, boolean isBooked, LocalDateTime date, BigDecimal cost) {
		this.id = id;
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.cost = cost;
	}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal cost) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.cost = cost;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public void setBooked(boolean booked) {
		isBooked = booked;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Seat))
			return false;
		if (obj == this)
			return true;

		Seat other = (Seat) obj;

		return new EqualsBuilder()
				.append(label, other.label)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(label)
				.hashCode();
	}
}
