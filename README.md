Title: JPowerAdmin
Project: JPowerAdmin
Author: nicmus inc. (jsabev@nicmus.com)
Web: https://www.jpoweradmin.ca

## JPowerAdmin

Control panel for PowerDNS 4.x.x based on Java EE blueprints. JPowerAdmin uses 
PrimeFaces as the front end JSF component framework. 

## System Requirements

JPowerAmin requires Java 8 - Java SDK 1.8 and Maven 3.0. The WAR file 
produced by this project is tested on Wildfly application server 9.x.x, 10.x.x
and 11.x.x. Other Java EE compliant application servers should work with 
minimal configuration changes.

## Building the WAR file

The WAR file can be build by invoking
	
	mvn clean compile war:war

## Configuration

### Pre-requisites

JPowerAdmin requires a DB schema as required by PowerDNS 4.x.x. Refer to the
PowerDNS documentation for the various DB schema. JPowerAdmin was tested with 
the generic MySQL backend. 

### Installing the JPowerAdmin schema

In the `resources` directory, there is a MySQL dump file containing the MySQL 
schema for JPowerAdmin control panel. Import the schema using `mysqldump`.

If not using MySQL, you can auto-create the JPowerAdmin schema using JPA. Set 
the `hibernate.hbm2ddl.auto` property to `create` in `resources/persistence.xml`

	`hibernate.hbm2ddl.auto=create`

*Warning:* `create` destroys existing data (if any)

### Deploying JPowerAdmin

The instructions below apply to Wildfly application server. Adjust accordingly.

1. Deploy the correct JDBC driver for the control panel.
2. Deploy the correct JDBC driver for your installation of PowerDNS (if different)
3. Create the following *XA datasources*:
 1. jndi-name="java:jboss/datasources/JPowerAdminDSXA"
 2. jndi-name="java:jboss/datasources/PowerDNSDSXA"
4. In `standalone.xml` create a security domain named `jpoweradmin`. In the 
security-domains subsystem copy and paste the following fragment:
```

                <security-domain name="jpoweradmin">
                    <authentication>
                        <login-module code="Database" flag="required">
                            <module-option name="dsJndiName" value="java:jboss/datasources/JPowerAdminDSXA"/>
                            <module-option name="principalsQuery" value="select password from User where deleted=FALSE and username=?"/>
                            <module-option name="rolesQuery" value="select role,'Roles' from Role join User on (Role.user_id = User.id) where User.username=?"/>
                            <module-option name="hashAlgorithm" value="SHA-256"/>
                            <module-option name="hashEncoding" value="base64"/>
                        </login-module>
                    </authentication>
                </security-domain>

```

JPowerAdmin is ready to be deployed. 

After deployment, navigate to http://<hostname>:8080/jpower-admin/register.jsf

The account username `root` is reserved for the root-privileged account.
All other usernames will have USER privileges.

