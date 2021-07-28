package models;

import d3e.core.CloneContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "Customer")
@Entity
public class Customer extends CreatableObject {
  @Field @NotNull private String name;
  @Field @NotNull private LocalDateTime dob;

  @Field
  @ColumnDefault("false")
  private boolean isUnderAge = false;

  @Field
  @ManyToOne(fetch = FetchType.LAZY)
  private Customer guardian;

  private transient Customer old;

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    onPropertySet();
    this.name = name;
  }

  public LocalDateTime getDob() {
    return this.dob;
  }

  public void setDob(LocalDateTime dob) {
    onPropertySet();
    this.dob = dob;
  }

  public boolean isIsUnderAge() {
    return this.isUnderAge;
  }

  public void setIsUnderAge(boolean isUnderAge) {
    onPropertySet();
    this.isUnderAge = isUnderAge;
  }

  public Customer getGuardian() {
    return this.guardian;
  }

  public void setGuardian(Customer guardian) {
    onPropertySet();
    this.guardian = guardian;
  }

  public Customer getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((Customer) old);
  }

  public String displayName() {
    return "Customer";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof Customer && super.equals(a);
  }

  public Customer deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    Customer _obj = ((Customer) dbObj);
    _obj.setName(name);
    _obj.setDob(dob);
    _obj.setIsUnderAge(isUnderAge);
    _obj.setGuardian(guardian);
  }

  public Customer cloneInstance(Customer cloneObj) {
    if (cloneObj == null) {
      cloneObj = new Customer();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setName(this.getName());
    cloneObj.setDob(this.getDob());
    cloneObj.setIsUnderAge(this.isIsUnderAge());
    cloneObj.setGuardian(this.getGuardian());
    return cloneObj;
  }

  public Customer createNewInstance() {
    return new Customer();
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.add(this.guardian);
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
