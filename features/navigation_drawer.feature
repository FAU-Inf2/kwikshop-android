Feature: Navigation Drawer

	Scenario: Open list of shopping lists
		Given I skipped the login
		Given I opened the navigation drawer
		Then I press "Shopping list"
		#make sure the navigation drawer works in this screen
		Given I opened the navigation drawer
		
	Scenario: Open list of recipies		
		Given I opened the navigation drawer
		Then I press "Recipes"
		#skip creation of default recips
		Then I press "No" 								
		#make sure the navigation drawer works in this screen
		Given I opened the navigation drawer 	
					
	Scenario: Open Supermarket Finder	
		Given I opened the navigation drawer
		Then I press "Supermarket Finder"
		Then I wait for the "Find nearby Supermarkets." dialog to close		
		#make sure the navigation drawer works in this screen
		Given I opened the navigation drawer	
		
	Scenario: Open settings
		Given I opened the navigation drawer
		Then I press "Settings"
		Then I should see "Settings"
		#make sure the navigation drawer works in this screen
		Given I opened the navigation drawer	
		
	Scenario: Open About screen		
		Given I opened the navigation drawer
		Then I press "About"
		Then I should see "About"	
		#make sure the navigation drawer works in this screen
		Given I opened the navigation drawer	
				
	Scenario: Open Login-Screen		
		Given I opened the navigation drawer
		Then I press "Login"
		Then I should see "Sign in with Google"
		