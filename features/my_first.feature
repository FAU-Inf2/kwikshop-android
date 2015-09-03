Feature: Add ShoppingList

	Scenario: Create a shopping list		 		
		Then I press view with id "login_skip_button"
		Then I should see "Are you sure"
		Then I press "OK"
		Then I press view with id "fab"
		Then I enter "New Shopping List" into input field number 1
		Then I press view with id "button_save"
		