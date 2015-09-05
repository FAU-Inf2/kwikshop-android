Feature: List of Recipes 

	Scenario: App offers to create default recipes on first run, user chooses not to create them
		Given I skipped the login
		Given I opened the list of recipes
		Then I should see "You do not have any recipes yet"
		Then I press "No"
		# see if the "Chili con carne" recipe has been created
		Then I should not see "Chili con carne"
		
	Scenario: App offers to create default recipes if there are no recipes, user chooses to create them
		Given I opened the list of recipes		
		Then I should see "You do not have any recipes yet"
		Then I press "Yes"
		# see if the "Chili con carne" recipe has been created
		Then I should see "Chili con carne"
				
	Scenario: Start creating recipe but cancel
		Given I opened the list of recipes
		Then I press the floating action button
		Then I enter "Test-Recipe" into input field number 1
		Then I go back
		Then I go back
		Then I should not see "Test-Recipe"
				
	Scenario: Create Recipe
		Given I opened the list of recipes
		Then I press the floating action button
		Then I enter "Test-Recipe" into input field number 1
		Then I press the save button
		# the recipe should now be opened, we should not be back in list of recipes		
		Then I should see "Test-Recipe"
		And I should not see "Chili con carne"
		Then I go back
		Then I should see "Test-Recipe"
		And I should see "Chili con carne"
				
	Scenario: Rename Recipe
		Given I opened the list of recipes
		Then I should see "Test-Recipe"
		Then I long press "Test-Recipe"
		Then I enter "Renamed Recipe" into input field number 1
		Then I press the save button		
		
	Scenario: Change Recipe scale
		Given I opened the list of recipes
		Then I long press "Renamed Recipe"
		Then I should see "people"
		Then I press "people"
		Then I should see "people"
		And I should see "pieces"
		Then I press "pieces"
		Then I should not see "people"
		Then I press the save button
		# we should be back in the list of recipes
		Then I should see "Chili con carne"
		Then I long press "Renamed Recipe"
		Then I should see "pieces"
				
	Scenario: Delete Recipe but cancel in confirmation dialog
		Given I opened the list of recipes
		Then I long press "Renamed Recipe"
		Then I press the delete button
		Then I should see "Are you sure"
		Then I press "No"
		# we should still be in the recipe details view
		Then I should see "Renamed Recipe"
		And I should see "pieces"		
		
	Scenario: Delete Recipe
		Given I opened the list of recipes
		Then I long press "Renamed Recipe"
		Then I press the delete button
		Then I should see "Are you sure"
		Then I press "Yes"
		# we should be back in list of recipes
		Then I should see "Chili con carne"
		And I should not see "Renamed Recipe"