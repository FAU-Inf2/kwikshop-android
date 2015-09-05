Feature: Shopping List

	Scenario: Add Item using quick-add
	# TODO
		Given I skipped the login	
		
		
	Scenario: Add Item using quick-add with enter button			
		Given I created a shopping list named "Test-ShoppingList"
		Given I opened the list of shopping lists
		Then I press "Test-ShoppingList"
		Then I press "Cancel"				
		Then I enter "Item A" into input field number 1
		Then I press the enter button
		Then I enter "Item B" into input field number 1		
		Then I should see "Item A"
		
# TODO: Scenario deactivated  	
#	Scenario: Delete Item 	
#		Given I opened the list of shopping lists
#		Then I press "Test-ShoppingList"	
#		Then I press "Cancel"	
#		Then I press "Item A"
#		# TODO: For some reason pressing the delete button does not work using calabash
#		Then I press the delete button
#		Then I press "Yes"
#		Then I should not see "Item A"		

	Scenario: Add Item using floating action button
		Given I opened the list of shopping lists
		Then I press "Test-ShoppingList"		
		Then I press "Cancel"	
		Then I press the floating action button
		Then I enter "Item E" into input field number 1
		Then I press the save button 
		Then I should see "Item E"
		And I should see "Item A"
		
	Scenario: Edit Item, change name
		Given I opened the list of shopping lists
		Then I press "Test-ShoppingList"
		Then I press "Cancel"	
		Then I should see "Item E"
		Then I press "Item E"
		Then I clear input field number 1
		Then I enter "Item E edited" into input field number 1
		Then I press the save button
		Then I should see "Item E edited"
		And I should see "Item A"
				
	Scenario: Edit Item, change unit
		Given I opened the list of shopping lists
		Then I press "Test-ShoppingList"	
		Then I press "Cancel"		
		Then I press "Item E edited"
		Then I press "pieces"
		Then I press "packs"
		Then I press the save button
		Then I press "Item E edited"
		Then I should see "packs"
		
	Scenario: Edit Item, change group
		Given I opened the list of shopping lists
		Then I press "Test-ShoppingList"	
		Then I press "Cancel"		
		Then I press "Item E edited"
		Then I press "Other"
		Then I press "Tobacco"
		Then I press the save button
		Then I press "Item E edited"
		Then I should see "Tobacco"	
				

		
		