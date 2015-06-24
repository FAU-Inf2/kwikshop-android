package de.fau.cs.mad.kwikshop.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
/*@NamedQueries({
        @NamedQuery(
                name = "User.findByLastName",
                query = "SELECT u FROM User u WHERE u.lastName = :lastName"
        )
})*/
public class User {

    @Id
    private String id;

    /*@Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;*/

    @Column(nullable = false)
    private String sessionToken;

    public User() {
    }

    public User(String id) {
        this.id = id;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*@JsonProperty
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }*/

    @JsonProperty
    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        final User user = (User) o;

        if (id != user.id
            || !firstName.equals(user.firstName)
            ||!lastName.equals(user.lastName)) return false;

        return true;
    }

    public void copy(User user) {
        firstName = user.firstName;
        lastName = user.lastName;
    }*/
}
