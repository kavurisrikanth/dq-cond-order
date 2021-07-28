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

  public static Comparable getOrderBy(Customer c, SortedCustomersUsingInput3Request _in) {
    CustomerSortOptions sortBy = _in.sortBy;
    return sortBy == CustomerSortOptions.AGE
        ? (c.isIsUnderAge() ? c.getGuardian().getAgeInYears() : c.getAgeInYears())
        : (sortBy == CustomerSortOptions.NAME ? c.getName() : c.getAgeInYears());
  }
}
