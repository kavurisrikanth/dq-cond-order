package helpers;

import classes.CustomerUtil;
import java.time.LocalDateTime;
import models.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.CustomerRepository;
import rest.GraphQLInputContext;
import store.EntityHelper;
import store.EntityMutator;
import store.EntityValidationContext;

@Service("Customer")
public class CustomerEntityHelper<T extends Customer> implements EntityHelper<T> {
  @Autowired protected EntityMutator mutator;
  @Autowired private CustomerRepository customerRepository;

  public void setMutator(EntityMutator obj) {
    mutator = obj;
  }

  public Customer newInstance() {
    return new Customer();
  }

  @Override
  public void fromInput(T entity, GraphQLInputContext ctx) {
    if (ctx.has("name")) {
      entity.setName(ctx.readString("name"));
    }
    if (ctx.has("dob")) {
      entity.setDob(ctx.readDateTime("dob"));
    }
    if (ctx.has("guardian")) {
      entity.setGuardian(ctx.readRef("guardian", "Customer"));
    }
    entity.updateMasters((o) -> {});
  }

  public void referenceFromValidations(T entity, EntityValidationContext validationContext) {}

  public void validateEntity(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    if (onCreate || onUpdate) {
      if (!(!entity.isIsUnderAge()
          || (entity.getGuardian() != null && !entity.getGuardian().isIsUnderAge()))) {
        validationContext.addEntityError("Guardian cannot be underage");
      }
    }
  }

  public void validateFieldName(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    String it = entity.getName();
    if (it == null) {
      validationContext.addFieldError("name", "name is required.");
      return;
    }
  }

  public void validateFieldDob(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    LocalDateTime it = entity.getDob();
    if (it == null) {
      validationContext.addFieldError("dob", "dob is required.");
      return;
    }
  }

  public void validateFieldGuardian(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    Customer it = entity.getGuardian();
    if (it == null) {
      if (isGuardianExists(entity)) {
        validationContext.addFieldError("guardian", "guardian is required.");
      } else {
        entity.setGuardian(null);
      }
      return;
    }
  }

  public void validateInternal(
      T entity, EntityValidationContext validationContext, boolean onCreate, boolean onUpdate) {
    validateEntity(entity, validationContext, onCreate, onUpdate);
    validateFieldName(entity, validationContext, onCreate, onUpdate);
    validateFieldDob(entity, validationContext, onCreate, onUpdate);
    if (isGuardianExists(entity)) {
      validateFieldGuardian(entity, validationContext, onCreate, onUpdate);
    }
    isGuardianExists(entity);
  }

  public void validateOnCreate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, true, false);
  }

  public void validateOnUpdate(T entity, EntityValidationContext validationContext) {
    validateInternal(entity, validationContext, false, true);
  }

  public void computeIsUnderAge(T entity) {
    try {
      entity.setIsUnderAge(CustomerUtil.isUnderAge(entity));
    } catch (RuntimeException e) {
    }
  }

  public boolean isGuardianExists(T entity) {
    try {
      if (entity.isIsUnderAge()) {
        return true;
      } else {
        entity.setGuardian(null);
        return false;
      }
    } catch (RuntimeException e) {
      return false;
    }
  }

  @Override
  public T clone(T entity) {
    return null;
  }

  @Override
  public T getById(long id) {
    return id == 0l ? null : ((T) customerRepository.findById(id).orElse(null));
  }

  @Override
  public void setDefaults(T entity) {}

  @Override
  public void compute(T entity) {
    this.computeIsUnderAge(entity);
  }

  private void deleteGuardianInCustomer(T entity, EntityValidationContext deletionContext) {
    if (EntityHelper.haveUnDeleted(this.customerRepository.getByGuardian(entity))) {
      deletionContext.addEntityError(
          "This Customer cannot be deleted as it is being referred to by Customer.");
    }
  }

  public Boolean onDelete(T entity, boolean internal, EntityValidationContext deletionContext) {
    return true;
  }

  public void validateOnDelete(T entity, EntityValidationContext deletionContext) {
    this.deleteGuardianInCustomer(entity, deletionContext);
  }

  @Override
  public Boolean onCreate(T entity, boolean internal) {
    return true;
  }

  @Override
  public Boolean onUpdate(T entity, boolean internal) {
    return true;
  }

  public T getOld(long id) {
    return ((T) getById(id).clone());
  }
}
