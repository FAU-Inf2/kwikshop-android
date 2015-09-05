Feature: Manage Shopping Lists

	Scenario: Create a shopping list		 		
		Given I skipped the login
		Then I press view with id "fab"
		Then I enter "New Shopping List" into input field number 1
		Then I press view with id "button_save"
		# Ship localization window
		Then I should see "Localization"
		Then I press "Cancel"
		# Go back to list of shopping lists
		Then I go back
		Then I should see "New Shopping List"
		
	Scenario: Cancelling renaming of shopping list does not change the list name
		Then I should see "New Shopping List"
		Then I long press "New Shopping List"
		Then I enter "Renamed Shopping List" into input field number 1		
		Then I go back		
		Then I go back  
		Then I should see "New Shopping List"
		And I should not see "Renamed Shopping List"
		
	Scenario: Rename Shopping List, Save goes back to list of shopping lists
		Then I long press "New Shopping List"
		Then I enter "Renamed Shopping List" into input field number 1
		Then I press view with id "button_save"
		Then I should see "Renamed Shopping List"
		And I should see "My First Shopping List"
		
	Scenario: Delete Shopping List
		Then I long press "Renamed Shopping List"
		Then I press view with id "button_remove"
		Then I should see "Are you sure"
		Then I press "Yes"
		Then I should see "My first shopping list"
		Then I should not see "Renamed Shopping List"