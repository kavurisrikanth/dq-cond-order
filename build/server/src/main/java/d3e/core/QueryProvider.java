package d3e.core;

import classes.LoginResult;
import classes.SortedCustomers;
import classes.SortedCustomersUsingInput;
import classes.SortedCustomersUsingInput2;
import classes.SortedCustomersUsingInput2Request;
import classes.SortedCustomersUsingInput3;
import classes.SortedCustomersUsingInput3Request;
import classes.SortedCustomersUsingInputRequest;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lists.SortedCustomersImpl;
import lists.SortedCustomersUsingInput2Impl;
import lists.SortedCustomersUsingInput3Impl;
import lists.SortedCustomersUsingInputImpl;
import models.AnonymousUser;
import models.Customer;
import models.OneTimePassword;
import models.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.AvatarRepository;
import repository.jpa.CustomerRepository;
import repository.jpa.OneTimePasswordRepository;
import repository.jpa.ReportConfigOptionRepository;
import repository.jpa.ReportConfigRepository;
import repository.jpa.UserRepository;
import repository.jpa.UserSessionRepository;
import security.AppSessionProvider;
import security.JwtTokenUtil;

@Service
public class QueryProvider {
  public static QueryProvider instance;
  @Autowired private JwtTokenUtil jwtTokenUtil;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private AvatarRepository avatarRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private ReportConfigRepository reportConfigRepository;
  @Autowired private ReportConfigOptionRepository reportConfigOptionRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private SortedCustomersImpl sortedCustomersImpl;
  @Autowired private SortedCustomersUsingInputImpl sortedCustomersUsingInputImpl;
  @Autowired private SortedCustomersUsingInput2Impl sortedCustomersUsingInput2Impl;
  @Autowired private SortedCustomersUsingInput3Impl sortedCustomersUsingInput3Impl;
  @Autowired private ObjectFactory<AppSessionProvider> provider;

  @PostConstruct
  public void init() {
    instance = this;
  }

  public static QueryProvider get() {
    return instance;
  }

  public AnonymousUser getAnonymousUserById(long id) {
    Optional<AnonymousUser> findById = anonymousUserRepository.findById(id);
    return findById.orElse(null);
  }

  public Customer getCustomerById(long id) {
    Optional<Customer> findById = customerRepository.findById(id);
    return findById.orElse(null);
  }

  public OneTimePassword getOneTimePasswordById(long id) {
    Optional<OneTimePassword> findById = oneTimePasswordRepository.findById(id);
    return findById.orElse(null);
  }

  public boolean checkTokenUniqueInOneTimePassword(long oneTimePasswordId, String token) {
    return oneTimePasswordRepository.checkTokenUnique(oneTimePasswordId, token);
  }

  public SortedCustomers getSortedCustomers() {
    return sortedCustomersImpl.get();
  }

  public SortedCustomersUsingInput getSortedCustomersUsingInput(
      SortedCustomersUsingInputRequest inputs) {
    return sortedCustomersUsingInputImpl.get(inputs);
  }

  public SortedCustomersUsingInput2 getSortedCustomersUsingInput2(
      SortedCustomersUsingInput2Request inputs) {
    return sortedCustomersUsingInput2Impl.get(inputs);
  }

  public SortedCustomersUsingInput3 getSortedCustomersUsingInput3(
      SortedCustomersUsingInput3Request inputs) {
    return sortedCustomersUsingInput3Impl.get(inputs);
  }

  public LoginResult loginWithOTP(String token, String code, String deviceToken) {
    OneTimePassword otp = oneTimePasswordRepository.getByToken(token);
    User user = otp.getUser();
    LoginResult loginResult = new LoginResult();
    if (deviceToken != null) {
      user.setDeviceToken(deviceToken);
    }
    loginResult.success = true;
    loginResult.userObject = otp.getUser();
    loginResult.token = token;
    return loginResult;
  }

  public AnonymousUser currentAnonymousUser() {
    return provider.getObject().getAnonymousUser();
  }
}
