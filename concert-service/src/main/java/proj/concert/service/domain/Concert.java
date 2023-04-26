package proj.concert.service.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.FetchType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.checkerframework.checker.units.qual.Length;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;

@Entity
@Table(name = "CONCERTS")
public class Concert implements Comparable<Concert> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    @Column(name = "BLURB", length = 1000)
    private String blurb;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "DATE")
    private Set<LocalDateTime> dates = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name = "CONCERT_ID"), inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID", referencedColumnName = "ID"))
    private Set<Performer> performers = new HashSet<>();

    public Concert() {
    }

    public Concert(Long id, String title, String imageName, String blurb) {
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
    }

    public Concert(String title, String imageName, String blurb) {
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Concert, id: ");
        buffer.append(id);
        buffer.append(", title: ");
        buffer.append(title);
        buffer.append(", imageName: ");
        buffer.append(imageName);
        buffer.append(", blurb: ");
        buffer.append(blurb);
        buffer.append(", dates: ");
        buffer.append(dates);
        buffer.append(", performers: ");
        buffer.append(performers);

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().append(title, rhs.title).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(title).hashCode();
    }

    @Override
    public int compareTo(Concert other) {
        return title.compareTo(other.getTitle());
    }
}