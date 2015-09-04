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


