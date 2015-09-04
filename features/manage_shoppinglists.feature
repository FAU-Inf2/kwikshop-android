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
		
		
	Scenario: Delete Shopping List
		Then I long press "New Shopping List"
		Then I press view with id "button_remove"
		Then I should see "Are you sure"
		Then I press "Yes"
		Then I should see "My first shopping list"
		Then I should not see "New Shopping List"