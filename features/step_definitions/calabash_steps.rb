require 'calabash-android/calabash_steps'

Then(/^"(.*?)" radio button should be selected$/) do |arg1|
  if(!query("RadioButton text:'#{arg1}'", :checked).first())
  	fail("The radio button with text #{arg1} should be selected")
  end
end

Then(/^I select "(.*?)" radio button$/) do |arg1|
  touch("RadioButton text:'#{arg1}'")
end

Then(/^"(.*?)" radio button should not be selected$/) do |arg1|
  if(query("RadioButton text:'#{arg1}'", :checked).first())
  	fail("The radio button with text #{arg1} should not be selected")
  end
end


Then /I wait for the "([^\"]*)" dialog to close/ do |text|
  # See if the dialog exists right now...
  unless query( "textview text:'#{text}'" ).length == 0
    # If it does, then wait for it to close...
    wait_for( timeout: 60 ) { 0 == query( "textview text:'#{text}'" ).length }
  end
end

Given /^I skipped the login$/ do
    macro 'I press view with id "login_skip_button"'
		macro 'I should see "Are you sure"'
		macro 'I press "OK"'
end

Given /^I opened the navigation drawer$/ do
    macro 'I press "Navigate up"'	
    macro 'I wait for the view with id "drawer_layout" to appear'	
end

Then /^I press the floating action button$/ do
    macro 'I press view with id "fab"'
end

Then(/^I press the save button$/) do
  macro 'I press view with id "button_save"'
end

Then(/^I press the delete button$/) do
  macro 'I press view with id "button_remove"'
end

Given(/^I opened the list of recipes$/) do
    macro 'I press "Navigate up"'	
    macro 'I wait for the view with id "drawer_layout" to appear'
    macro 'I press "Recipes"'	
end

Given(/^I created a recipe named "(.*?)"$/) do |name|
    macro 'I press "Navigate up"'	
    macro 'I wait for the view with id "drawer_layout" to appear'
    macro 'I press "Recipes"'	
    macro 'I press the floating action button'
		macro "I enter \"#{name}\" into input field number 1"
    macro 'I press the save button'
		macro 'I go back'    		
end

Given(/^I did not create the default recipes$/) do
    macro 'I press "Navigate up"'	
    macro 'I wait for the view with id "drawer_layout" to appear'
    macro 'I press "Recipes"'	
    macro 'I press "Do not show me again"'
    macro 'I press "No"'
end 

Then(/^I press the quick-add button$/) do
  macro 'I press view with id "button_quickAdd"'
end


Given(/^I created a shopping list named "(.*?)"$/) do |name|
    macro 'I press "Navigate up"'	
    macro 'I wait for the view with id "drawer_layout" to appear'
    macro 'I press "Shopping list"'	
    macro 'I press the floating action button'
		macro "I enter \"#{name}\" into input field number 1"
    macro 'I press the save button'
    macro 'I press "Cancel"'
		macro 'I go back'    		
end

Given(/^I opened the list of shopping lists$/) do
    macro 'I press "Navigate up"'	
    macro 'I wait for the view with id "drawer_layout" to appear'
    macro 'I press "Shopping list"'	
end