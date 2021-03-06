Feature: Recipe

	Scenario: Add Item using quick-add
		Given I skipped the login
		Given I did not create the default recipes
		Given I created a recipe named "Test-Recipe"
		Given I opened the list of recipes
		Then I press "Test-Recipe"				
		Then I enter "Item A" into input field number 1
		Then I press the quick-add button
		Then I enter "Item B" into input field number 1		
		Then I should see "Item A"
		
# TODO: Scenario deactivated  	
#	Scenario: Delete Item 	
#		Given I opened the list of recipes
#		Then I press "Test-Recipe"		
#		Then I press "Item A"
#		# TODO: For some reason pressing the delete button does not work using calabash
#		Then I press the delete button
#		Then I press "Yes"
#		Then I should not see "Item A"		
#		And I should see "Item C"
							
	Scenario: Add Item using quick-add with enter button
		Given I opened the list of recipes
		Then I press "Test-Recipe"
		Then I enter "Item C" into input field number 1
		Then I press the enter button
		Then I enter "Item D" into input field number 1
		Then I should see "Item C"
				
	Scenario: Add Item using floating action button
		Given I opened the list of recipes
		Then I press "Test-Recipe"
		Then I press the floating action button
		Then I enter "Item E" into input field number 1
		Then I press the save button 
		Then I should see "Item E"
		And I should see "Item C"
		
	Scenario: Edit Item, change name
		Given I opened the list of recipes
		Then I press "Test-Recipe"
		Then I should see "Item E"
		Then I press "Item E"
		Then I clear input field number 1
		Then I enter "Item E edited" into input field number 1
		Then I press the save button
		Then I should see "Item E edited"
		And I should see "Item C"
		
# 	TODO: Scenario deactivated  						 
#	Scenario: Edit Item, change unit from piece to pack
#		Given I opened the list of recipes
#		Then I press "Test-Recipe"				
#		Then I press "Item E edited"
#		Then I select "pack" from picker number 1
#		Then I press the save button
#		Then I press "Item E edited"
#		Then I should see "pack"
		
	Scenario: Edit Item, change group
		Given I opened the list of recipes
		Then I press "Test-Recipe"		
		Then I press "Item E edited"
		Then I press "Other"
		Then I press "Tobacco"
		Then I press the save button
		Then I press "Item E edited"
		Then I should see "Tobacco"	
				

		
		