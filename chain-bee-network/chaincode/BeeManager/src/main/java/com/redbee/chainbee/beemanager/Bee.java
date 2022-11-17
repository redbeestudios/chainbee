package com.redbee.chainbee.beemanager;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public final class Bee {

    @Property()
    private final String name;
    @Property()
    private final Integer id;
    @Property()
    private final String seniority;
    @Property()
    private final String manager;
    @Property()
    private final String assignment;
    @Property()
    private final boolean isActive;
    @Property()
    private final boolean hasMixedSalary;
    @Property()
    private final Double salary;

    public Double getSalary() {
        return this.salary;
    }

    public String getName() {
        return this.name;
    }

    public Integer getId() {
        return this.id;
    }

    public String getSeniority() {
        return this.seniority;
    }

    public String getManager() {
        return this.manager;
    }

    public String getAssignment() {
        return this.assignment;
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public boolean getHasMixedSalary() {
        return this.hasMixedSalary;
    }

    public Bee(@JsonProperty("isActive") final boolean isActive, @JsonProperty("name") final String name, @JsonProperty("id") final Integer id,
               @JsonProperty("seniority") final String seniority, @JsonProperty("manager") final String manager, @JsonProperty("assignment") final String assignment,
               @JsonProperty("hasMixedSalary") final boolean hasMixedSalary, @JsonProperty("salary") final Double salary) {
        this.isActive = isActive;
        this.name = name;
        this.id = id;
        this.seniority = seniority;
        this.manager = manager;
        this.assignment = assignment;
        this.hasMixedSalary = hasMixedSalary;
        this.salary = salary;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        Bee other = (Bee) obj;
        return Objects.deepEquals(new String[]{String.valueOf(getIsActive()), getName(), getId().toString(), getSeniority(),
                getManager(), getAssignment(), String.valueOf(getHasMixedSalary()), getSalary().toString()},
            new String[]{String.valueOf(other.getIsActive()), other.getName(), other.getId().toString(), other.getSeniority(),
                other.getManager(), other.getAssignment(), String.valueOf(other.getHasMixedSalary()), other.getSalary().toString()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIsActive(), getName(), getId(), getSeniority(),
            getManager(), getAssignment(), getHasMixedSalary(), getSalary());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [id=" + this.id
            + ", name=" + this.name + ", isActive=" + this.isActive + ", seniority=" + this.seniority + ", manager=" + this.manager +
            ", assignment=" + this.assignment + ", hasMixedSalary=" + this.hasMixedSalary + ", salary=" + this.salary + "]";
    }

}
