Feature: ValueParcel behavior

  Scenario Outline: Parcel Type Test
    Given a new <parcelName>
    When I put in a value:
      """
      {
        "name" : "in-parcel-object",
        "value" : 11
      }
      """
    Then I can put other value:
      """
      {
        "name" : "in-parcel-object",
        "value" : 43
      }
      """
    When I seal the parcel
    Then I can't put value in it
    When I reveal the parcel
    Then the name should be "in-parcel-object"
    And the value should be 43

  Examples:
    | parcelName |
    | ValueParcel |