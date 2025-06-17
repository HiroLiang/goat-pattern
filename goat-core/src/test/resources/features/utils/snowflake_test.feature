Feature: Snow Flake Generator

  Scenario Outline:
    Given a snow flake generator
    When I create <number> identities
    Then I have <number> identities in Set

  Examples:
    |number|
    |1000000|