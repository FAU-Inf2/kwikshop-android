package io.dropwizard.hibernate;

// Dummy @UnitOfWork annotation so app does not need dropwizard dependency while still sharing interfaces.
// Server uses dropwizard directly
public @interface UnitOfWork {
}
