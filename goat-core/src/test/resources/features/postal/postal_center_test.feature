Feature: Test Postal Center Mechanism

  Scenario:
    Given a postal center
    When I register two mailbox
    And deliver a test parcel with word "test"
    Then the other mailbox should get a parcel contains word "test"
    When I register a group "group"
    And deliver a test parcel with word "test" with group "group"
    Then the other mailbox should get a parcel contains word "test"
    When I unregister group "group"
    And deliver a test parcel with word "test" with group "group"
    Then the other mailbox can't get parcel with word "test"
    When I unregister mailbox
    Then I can not deliver parcel