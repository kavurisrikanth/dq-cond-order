package classes;

import java.time.LocalDateTime;
import models.Customer;

public class CustomerUtil {
  public CustomerUtil() {}

  public static boolean isUnderAge(Customer c) {
    LocalDateTime now = LocalDateTime.now();
    long nowYear = now.getYear();
    long cYear = c.getDob().getYear();
    c.setAgeInYears(nowYear - cYear);
    return c.getAgeInYears() < 18l;
  }
}
