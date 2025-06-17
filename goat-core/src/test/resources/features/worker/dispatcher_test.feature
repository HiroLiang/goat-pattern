Feature: Test Queue Dispatch Worker

  Scenario:
    Given a Dispatch worker
    When I submit dispatcher a str "word"
    Then words should contain "word"
    When I submit dispatcher the following strings:
      |Bob|
      |John|
      |Jack|
    Then words should contain the following strings:
      |Bob|
      |John|
      |Jack|
    When I offer dispatcher a str "word"
    Then words should contain "word"
    When I offer dispatcher the following strings:
      |Bob|
      |John|
      |Jack|
    Then words should contain the following strings:
      |Bob|
      |John|
      |Jack|
