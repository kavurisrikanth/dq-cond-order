package rest.ws;

import classes.LoginResult;
import classes.MutateResultStatus;
import d3e.core.CurrentUser;
import d3e.core.D3ELogger;
import d3e.core.ListExt;
import gqltosql.GqlToSql;
import graphql.language.Field;
import java.util.UUID;
import lists.SortedCustomersImpl;
import models.AnonymousUser;
import models.OneTimePassword;
import models.User;
import org.json.JSONObject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.OneTimePasswordRepository;
import security.AppSessionProvider;
import security.JwtTokenUtil;
import security.UserProxy;
import store.ValidationFailedException;

@Service
public class RocketQuery extends AbstractRocketQuery {
  @Autowired private GqlToSql gqlToSql;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectFactory<AppSessionProvider> provider;
  @Autowired private JwtTokenUtil jwtTokenUtil;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private SortedCustomersImpl sortedCustomersImpl;

  protected QueryResult executeOperation(String query, Field field, RocketInputContext ctx)
      throws Exception {
    D3ELogger.displayGraphQL(query, query, null);
    User currentUser = CurrentUser.get();
    switch (query) {
      case "getAnonymousUserById":
        {
          if (currentUser instanceof AnonymousUser) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList("Current user does not have read permissions for this model."));
          }
          return singleResult(
              "AnonymousUser", false, gqlToSql.execute("AnonymousUser", field, ctx.readLong()));
        }
      case "getCustomerById":
        {
          if (currentUser instanceof AnonymousUser) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList("Current user does not have read permissions for this model."));
          }
          return singleResult(
              "Customer", false, gqlToSql.execute("Customer", field, ctx.readLong()));
        }
      case "getOneTimePasswordById":
        {
          if (currentUser instanceof AnonymousUser) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList("Current user does not have read permissions for this model."));
          }
          return singleResult(
              "OneTimePassword", false, gqlToSql.execute("OneTimePassword", field, ctx.readLong()));
        }
      case "getSortedCustomers":
        {
          if (!(currentUser instanceof AnonymousUser)) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList(
                    "Current user type does not have read permissions for this DataQuery."));
          }
          JSONObject res = sortedCustomersImpl.getAsJson(inspect(field, "items"));
          return singleResult("SortedCustomers", false, res);
        }
      case "loginWithOTP":
        {
          String token = ctx.readString();
          String code = ctx.readString();
          String deviceToken = ctx.readString();
          return singleResult("LoginResult", false, loginWithOTP(field, token, code, deviceToken));
        }
      case "currentAnonymousUser":
        {
          return singleResult("AnonymousUser", false, provider.getObject().getAnonymousUser());
        }
    }
    D3ELogger.info("Query Not found");
    return null;
  }

  private LoginResult loginWithOTP(Field field, String token, String code, String deviceToken)
      throws Exception {
    OneTimePassword otp = oneTimePasswordRepository.getByToken(token);
    LoginResult loginResult = new LoginResult();
    if (otp == null) {
      loginResult.success = false;
      loginResult.failureMessage = "Invalid token.";
      return loginResult;
    }
    if (otp.getExpiry().isBefore(java.time.LocalDateTime.now())) {
      loginResult.success = false;
      loginResult.failureMessage = "OTP validity has expired.";
      return loginResult;
    }
    if (!(code.equals(otp.getCode()))) {
      loginResult.success = false;
      loginResult.failureMessage = "Invalid code.";
      return loginResult;
    }
    User user = ((User) org.hibernate.Hibernate.unproxy(otp.getUser()));
    if (user == null) {
      loginResult.success = false;
      loginResult.failureMessage = "Invalid user.";
      return loginResult;
    }
    loginResult.success = true;
    loginResult.userObject = user;
    String type = ((String) user.getClass().getSimpleName());
    String id = String.valueOf(user.getId());
    String finalToken =
        jwtTokenUtil.generateToken(
            id, new UserProxy(type, user.getId(), UUID.randomUUID().toString()));
    loginResult.token = finalToken;
    if (deviceToken != null) {
      user.setDeviceToken(deviceToken);
      store.Database.get().save(user);
    }
    return loginResult;
  }
}
