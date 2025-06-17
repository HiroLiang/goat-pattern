Feature: Test Abstract Worker can control work

  Scenario:
    Given a Test Work
    When I start work
    Then work should renew current every seconds
    When I stop work
    Then work should stop renewing current every seconds