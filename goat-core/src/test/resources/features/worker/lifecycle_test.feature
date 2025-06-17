Feature: Test Abstract Lifecycle options

  Scenario:
    Given a Test Lifecycle
    When I start lifecycle
    Then is running should be "true", is pause should bo "false"
    When I pause lifecycle
    Then is running should be "true", is pause should bo "true"
    When I resume lifecycle
    Then is running should be "true", is pause should bo "false"
    When I stop lifecycle
    Then is running should be "false", is pause should bo "false"
    When I destroy lifecycle
    Then is running should be "false", is pause should bo "false"