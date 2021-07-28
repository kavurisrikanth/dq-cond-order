package rest.ws;

import classes.MutateResultStatus;
import d3e.core.CurrentUser;
import d3e.core.D3ELogger;
import d3e.core.ListExt;
import gqltosql.schema.IModelSchema;
import helpers.CustomerEntityHelper;
import models.AnonymousUser;
import models.Customer;
import models.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.CustomerRepository;
import repository.jpa.OneTimePasswordRepository;
import repository.jpa.UserRepository;
import repository.jpa.UserSessionRepository;
import security.AppSessionProvider;
import store.DatabaseObject;
import store.EntityHelperService;
import store.EntityMutator;
import store.ValidationFailedException;

@org.springframework.stereotype.Service
public class RocketMutation extends AbstractRocketQuery {
  @Autowired private EntityMutator mutator;
  @Autowired private ObjectFactory<EntityHelperService> helperService;
  @Autowired private IModelSchema schema;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private ObjectFactory<AppSessionProvider> provider;

  public void save(String type, DatabaseObject obj) throws Exception {
    boolean create = obj.isNew();
    switch (type) {
      case "Customer":
        {
          saveCustomer(((Customer) obj), create);
          break;
        }
    }
    D3ELogger.info("Mutation Not found");
  }

  public void delete(String type, long id) throws Exception {
    switch (type) {
    }
    D3ELogger.info("Mutation Not found");
  }

  private void saveCustomer(Customer obj, boolean create) throws Exception {
    User currentUser = CurrentUser.get();
    if (create) {
      if (!(currentUser instanceof AnonymousUser)) {
        throw new ValidationFailedException(
            MutateResultStatus.AuthFail,
            ListExt.asList("Current user type does not have create permissions for this model."));
      }
    }
    CustomerEntityHelper customerHelper = this.mutator.getHelper("Customer");
    this.mutator.save(((Customer) obj), false);
  }
}
