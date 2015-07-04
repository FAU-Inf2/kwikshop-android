package de.fau.cs.mad.kwikshop.common;

import com.wordnik.swagger.annotations.*;

import javax.ws.rs.core.Response;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
/**
 * Created by Andreas Kumlehn on 3/26/15.
 */
@Path("users")
@Api(value = "users", description = "Operations on users")
public interface UserResource {

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            value = "Authenticate user via third-party login (e.g. Google)",
            notes = "Returns this user's session token",
            response = Response.class)
    String auth(@FormParam("token") String token);

    /*@GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Find user by id",
            notes = "Returns a user if it exists.",
            response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved user in body"),
            @ApiResponse(code = 400, message = "Invalid id supplied"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    User get(
            @ApiParam(value = "id of the desired user or now id for a list of all users", required = false)
            @PathParam("id")String id);

    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Find user(s)",
            notes = "Provide either:" +
                    "1. a list of ids (may be empty to get all users)" +
                    "2. first- and/or lastname",
            response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved list of users (may be empty)"),
            @ApiResponse(code = 400, message = "If both ids and first-/lastname are supplied"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    List<User> get(
            @ApiParam(value = "List of ids to search for", required = false)
            @QueryParam("id") List<String> ids,
            @ApiParam(value = "firstName to search for", required = false)
            @QueryParam("firstName") String firstName,
            @ApiParam(value = "firstName to search for", required = false)
            @QueryParam("lastName") String lastName);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create a new user",
            notes = "Inserts a new user into the database. Returns the new user object including id.",
            response = User.class)
    User createUser(@ApiParam(value = "User to create", required = true)User newUser);

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create a new user",
            notes = "Inserts a new user into the database. Returns the new user object including id.",
            response = User.class)
    User createUser(@ApiParam(value = "id of the new user", required = true)
                    @QueryParam("id") String id,
                    @ApiParam(value = "firstName of the new user", required = true)
                    @QueryParam("firstName") String firstName,
                    @ApiParam(value = "lastName of the new user", required = true)
                    @QueryParam("lastName") String lastName);

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update an existing user",
            notes = "Updates the details of an existing user. Returns the updated user object.",
            response = User.class)
    User updateUser(
            @ApiParam(value = "id of the user", required = true)
            @PathParam("id") String id,
            @ApiParam(value = "new details of the specified user", required = true) User user);

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Delete a user",
            notes = "Removes a user from the database.",
            response = User.class)
    void deleteUser(
            @ApiParam(value = "id of the to be deleted user", required = true)
            @PathParam("id") String id);*/
}
