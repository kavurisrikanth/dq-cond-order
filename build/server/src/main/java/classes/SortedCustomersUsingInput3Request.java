package classes;

import rest.GraphQLInputContext;

public class SortedCustomersUsingInput3Request implements rest.IGraphQLInput {
  public CustomerSortOptions sortBy;

  public SortedCustomersUsingInput3Request() {}

  public SortedCustomersUsingInput3Request(CustomerSortOptions sortBy) {
    this.sortBy = sortBy;
  }

  @Override
  public void fromInput(GraphQLInputContext ctx) {
    if (ctx.has("sortBy")) {
      sortBy = ctx.readEnum("sortBy", CustomerSortOptions.class);
    }
  }
}
