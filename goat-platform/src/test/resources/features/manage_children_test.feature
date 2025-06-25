Feature: Test to manage children

  Scenario:
    Given a root platform
    When I create a child platform
    Then root should has same class child
    When I destroy the child
    Then I can not find any child
    When I have two children in root
    And I ask to end child platform service
    Then it would destroy all children platform