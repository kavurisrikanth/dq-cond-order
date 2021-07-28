package classes;

import rest.GraphQLInputContext;

public class SortedCustomersUsingInput2Request implements rest.IGraphQLInput {
  public CustomerSortOptions sortBy;

  public SortedCustomersUsingInput2Request() {}

  public SortedCustomersUsingInput2Request(CustomerSortOptions sortBy) {
    this.sortBy = sortBy;
  }

  @Override
  public void fromInput(GraphQLInputContext ctx) {
    if (ctx.has("sortBy")) {
      sortBy = ctx.readEnum("sortBy", CustomerSortOptions.class);
    }
  }
}
