package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.field.DatabaseField;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;

/**
 * Stores information about a tuple of User/Server (used in synchronization)
 */
public class ConnectionInfo {

    /**
     * Id-field required by ORMLite
     */
    @DatabaseField(generatedId = true)
    @SuppressWarnings("unused")
    private int id;

    @DatabaseField
    private String userId;

    @DatabaseField
    private String apiEndpoint;



    /**
     * Empty constructor required by ORMLite
     */
    @SuppressWarnings("unused")
    public ConnectionInfo() {

    }

    public ConnectionInfo(String userId, String apiEndpoint) {

        if(userId == null) {
            throw new ArgumentNullException("userId");
        }

        if(apiEndpoint == null) {
            throw new ArgumentNullException("apiEndpoint");
        }

        this.userId = userId;
        this.apiEndpoint = apiEndpoint;
    }

    public ConnectionInfo(ConnectionInfo other) {

        this(other.getUserId(), other.getApiEndpoint());
    }



    public String getUserId() {
        return this.userId;
    }

    public String getApiEndpoint() {
        return this.apiEndpoint;
    }

    @Override
    public int hashCode() {
        return userId.hashCode() | apiEndpoint.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if(o != null && o instanceof ConnectionInfo) {
            ConnectionInfo other = (ConnectionInfo) o;

            return  other.getUserId().equals(this.userId) &&
                    other.getApiEndpoint().equals(this.getApiEndpoint());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s: userId = %s, apiEndPoint = %s",
                getClass().getSimpleName(),
                userId,
                apiEndpoint);
    }

}
