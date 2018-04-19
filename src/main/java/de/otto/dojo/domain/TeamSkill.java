package de.otto.dojo.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A TeamSkill.
 */
@Entity
@Table(name = "team_skill")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TeamSkill implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "achieved_at")
    private Instant achievedAt;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name = "note")
    private String note;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getAchievedAt() {
        return achievedAt;
    }

    public TeamSkill achievedAt(Instant achievedAt) {
        this.achievedAt = achievedAt;
        return this;
    }

    public void setAchievedAt(Instant achievedAt) {
        this.achievedAt = achievedAt;
    }

    public Boolean isVerified() {
        return verified;
    }

    public TeamSkill verified(Boolean verified) {
        this.verified = verified;
        return this;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getNote() {
        return note;
    }

    public TeamSkill note(String note) {
        this.note = note;
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TeamSkill teamSkill = (TeamSkill) o;
        if (teamSkill.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), teamSkill.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TeamSkill{" +
            "id=" + getId() +
            ", achievedAt='" + getAchievedAt() + "'" +
            ", verified='" + isVerified() + "'" +
            ", note='" + getNote() + "'" +
            "}";
    }
}
