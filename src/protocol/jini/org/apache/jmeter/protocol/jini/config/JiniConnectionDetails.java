package org.apache.jmeter.protocol.jini.config;

public class JiniConnectionDetails {

    private final String rmiRegistryUrl;
    private final String serviceName;
    private final String serviceInterface;

    public JiniConnectionDetails(String rmiRegistryUrl, String serviceName, String serviceInterface) {
        this.rmiRegistryUrl = rmiRegistryUrl;
        this.serviceName = serviceName;
        this.serviceInterface = serviceInterface;
    }

    public String getRmiRegistryUrl() {
        return rmiRegistryUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rmiRegistryUrl == null) ? 0 : rmiRegistryUrl.hashCode());
        result = prime * result + ((serviceInterface == null) ? 0 : serviceInterface.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JiniConnectionDetails other = (JiniConnectionDetails) obj;
        if (rmiRegistryUrl == null) {
            if (other.rmiRegistryUrl != null)
                return false;
        } else if (!rmiRegistryUrl.equals(other.rmiRegistryUrl))
            return false;
        if (serviceInterface == null) {
            if (other.serviceInterface != null)
                return false;
        } else if (!serviceInterface.equals(other.serviceInterface))
            return false;
        if (serviceName == null) {
            if (other.serviceName != null)
                return false;
        } else if (!serviceName.equals(other.serviceName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "JiniConnectionDetails [rmiRegistryUrl=" + rmiRegistryUrl + ", serviceName=" + serviceName + ", serviceInterface=" + serviceInterface + "]";
    }

}
