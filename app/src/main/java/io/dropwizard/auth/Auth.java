package io.dropwizard.auth;

// Dummy @Auth annotation so app does not need dropwizard dependency while still sharing interfaces.
// Server uses dropwizard directly
public @interface Auth {
}
