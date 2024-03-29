package rest;

import classes.MutateResultStatus;
import classes.SortedCustomersUsingInput2Request;
import classes.SortedCustomersUsingInputRequest;
import d3e.core.CurrentUser;
import d3e.core.D3ELogger;
import d3e.core.ListExt;
import gqltosql.GqlToSql;
import gqltosql.schema.IModelSchema;
import graphql.language.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lists.SortedCustomersImpl;
import lists.SortedCustomersUsingInput2Impl;
import lists.SortedCustomersUsingInput3Impl;
import lists.SortedCustomersUsingInputImpl;
import models.AnonymousUser;
import models.OneTimePassword;
import models.User;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.CustomerRepository;
import repository.jpa.OneTimePasswordRepository;
import repository.jpa.UserRepository;
import repository.jpa.UserSessionRepository;
import security.AppSessionProvider;
import security.JwtTokenUtil;
import security.UserProxy;
import store.EntityHelperService;
import store.EntityMutator;
import store.ValidationFailedException;

@RestController
@RequestMapping("api/native/")
public class NativeQuery extends AbstractQueryService {
  @Value("classpath:introspec.json")
  private Resource inrospecFile;

  @Autowired private EntityMutator mutator;
  @Autowired private ObjectFactory<EntityHelperService> helperService;
  @Autowired private GqlToSql gqlToSql;
  @Autowired private IModelSchema schema;
  @Autowired private JwtTokenUtil jwtTokenUtil;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectFactory<AppSessionProvider> provider;
  @Autowired private SortedCustomersImpl sortedCustomersImpl;
  @Autowired private SortedCustomersUsingInputImpl sortedCustomersUsingInputImpl;
  @Autowired private SortedCustomersUsingInput2Impl sortedCustomersUsingInput2Impl;
  @Autowired private SortedCustomersUsingInput3Impl sortedCustomersUsingInput3Impl;

  @PostMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  public String run(@RequestBody String query) throws Exception {
    JSONObject req = new JSONObject(query);
    List<Field> fields = parseFields(req);
    JSONObject variables = req.getJSONObject("variables");
    String queryStr = null;
    try {
      queryStr = req.getString("query");
    } catch (JSONException e) {
    }
    return executeFields(queryStr, fields, variables);
  }

  public String executeFields(String query, List<Field> fields, JSONObject variables)
      throws Exception {
    JSONObject data = new JSONObject();
    for (Field s : fields) {
      String name = s.getAlias() == null ? s.getName() : s.getAlias();
      long time = System.currentTimeMillis();
      D3ELogger.info("Started: " + time + ":" + s.getName());
      try {
        data.put(name, executeOperation(query, s, variables));
      } catch (ValidationFailedException e) {
        JSONObject errors = new JSONObject();
        errors.put("error", e.getErrors());
        data.put(name, errors);
        logErrors(errors);
      }
      D3ELogger.info("Completed: " + time + ":" + s.getName());
    }
    JSONObject output = new JSONObject();
    output.put("data", data);
    return output.toString();
  }

  protected Object executeOperation(String query, Field field, JSONObject variables)
      throws Exception {
    GraphQLInputContext ctx =
        new ArgumentInputContext(
            field.getArguments(),
            helperService.getObject(),
            new HashMap<>(),
            new HashMap<>(),
            variables);
    D3ELogger.displayGraphQL(field.getName(), query, variables);
    User currentUser = CurrentUser.get();
    switch (field.getName()) {
      case "__schema":
        {
          String json = IOUtils.toString(inrospecFile.getInputStream(), Charset.defaultCharset());
          return new JSONObject(json);
        }
      case "getAnonymousUserById":
        {
          if (currentUser instanceof AnonymousUser) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList("Current user does not have read permissions for this model."));
          }
          return gqlToSql.execute("AnonymousUser", field, ctx.readLong("id"));
        }
      case "getCustomerById":
        {
          if (currentUser instanceof AnonymousUser) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList("Current user does not have read permissions for this model."));
          }
          return gqlToSql.execute("Customer", field, ctx.readLong("id"));
        }
      case "getOneTimePasswordById":
        {
          if (currentUser instanceof AnonymousUser) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList("Current user does not have read permissions for this model."));
          }
          return gqlToSql.execute("OneTimePassword", field, ctx.readLong("id"));
        }
      case "getSortedCustomers":
        {
          if (!(currentUser instanceof AnonymousUser)) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList(
                    "Current user type does not have read permissions for this DataQuery."));
          }
          return sortedCustomersImpl.getAsJson(inspect(field, "items"));
        }
      case "getSortedCustomersUsingInput":
        {
          if (!(currentUser instanceof AnonymousUser)) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList(
                    "Current user type does not have read permissions for this DataQuery."));
          }
          SortedCustomersUsingInputRequest req =
              ctx.readInto("in", new SortedCustomersUsingInputRequest());
          return sortedCustomersUsingInputImpl.getAsJson(inspect(field, "items"), req);
        }
      case "getSortedCustomersUsingInput2":
        {
          if (!(currentUser instanceof AnonymousUser)) {
            throw new ValidationFailedException(
                MutateResultStatus.AuthFail,
                ListExt.asList(
                    "Current user type does not have read permissions for this DataQuery."));
          }
          SortedCustomersUsingInput2Request req =
              ctx.readInto("in", new SortedCustomersUsingInput2Request());
          return sortedCustomersUsingInput2Impl.getAsJson(inspect(field, "items"), req);
        }
      case "loginWithOTP":
        {
          String token = ctx.readString("token");
          String code = ctx.readString("code");
          String deviceToken = ctx.readString("deviceToken");
          return loginWithOTP(field, token, code, deviceToken);
        }
      case "currentAnonymousUser":
        {
          return currentAnonymousUser(field);
        }
    }
    D3ELogger.info("Query Not found");
    return null;
  }

  private JSONObject loginWithOTP(Field field, String token, String code, String deviceToken)
      throws Exception {
    OneTimePassword otp = oneTimePasswordRepository.getByToken(token);
    JSONObject loginResult = new JSONObject();
    if (otp == null) {
      loginResult.put("success", false);
      loginResult.put("loginResult", "Wrong password.");
      return loginResult;
    }
    if (otp.getExpiry().isBefore(java.time.LocalDateTime.now())) {
      loginResult.put("success", false);
      loginResult.put("loginResult", "Wrong password.");
      return loginResult;
    }
    if (!(code.equals(otp.getCode()))) {
      loginResult.put("success", false);
      loginResult.put("loginResult", "Wrong password.");
      return loginResult;
    }
    User user = ((User) org.hibernate.Hibernate.unproxy(otp.getUser()));
    if (user == null) {
      loginResult.put("success", false);
      loginResult.put("loginResult", "Wrong password.");
      return loginResult;
    }
    loginResult.put("success", true);
    JSONObject userObject = gqlToSql.execute("User", inspect(field, "userObject"), user.getId());
    loginResult.put("userObject", userObject);
    String type = ((String) userObject.get("__typename"));
    String id = String.valueOf(user.getId());
    String finalToken =
        jwtTokenUtil.generateToken(
            id, new UserProxy(type, user.getId(), UUID.randomUUID().toString()));
    loginResult.put("token", finalToken);
    if (deviceToken != null) {
      user.setDeviceToken(deviceToken);
      store.Database.get().save(user);
    }
    return loginResult;
  }

  private JSONObject currentAnonymousUser(Field field) throws Exception {
    AnonymousUser user = provider.getObject().getAnonymousUser();
    return gqlToSql.execute("AnonymousUser", field, user.getId());
  }
}
