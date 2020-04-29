# Spring Books Cloud Spanner R2DBC Sample

> A derivative work based upon the [cloud-spanner-spring-data-r2dbc-sample](https://github.com/GoogleCloudPlatform/cloud-spanner-r2dbc/tree/master/cloud-spanner-r2dbc-samples/cloud-spanner-spring-data-r2dbc-sample).

## Prerequisites

* A valid GCP account and [project](https://cloud.google.com/resource-manager/docs/creating-managing-projects) id
* [gcloud SDK](https://cloud.google.com/sdk/install)
* JDK/JRE 11

If you want to run this sample on a Cloud Foundry foundation or Kubernetes cluster with [cf-for-k8s](https://github.com/cloudfoundry/cf-for-k8s) installed, then you'll need the

* [cf CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html#pkg-linux)

and valid account credentials

In addition your platform operator must have installed and configured the [gcp-service-broker](https://github.com/GoogleCloudPlatform/gcp-service-broker).

## Clone

```
git clone https://github.com/pacphi/spring-books.git
```

## Build

```
cd spring-books
./gradlew build
```

## Run

This sample creates a table called `BOOK` on application startup, and deletes it prior to application shutdown.

### Locally

Run the sample from the command line, providing the following properties

* `spanner.instance`
* `spanner.database`
* `gcp.project`

```
./gradlew bootRun -Dspring-boot.run.jvmArguments="-Dspanner.instance=[SPANNER-INSTANCE] -Dspanner.database=[SPANNER-DATABASE] -Dgcp.project=GCP-PROJECT"
```

Visit http://localhost:8080/index.html in your favorite browser.


### on cf-for-k8s

Login to a foundation and target an organization and space
```
cf api {api-host}
cf auth {username} {password}
cf t -o {organization} -s {space}
```

Create a Google Spanner service instance

```
cf create-service google-spanner sandbox spanner-sandbox-instance
```

Fetch the instance id of the spanner-sandbox-instance

```
gcloud spanner instances list
```
> Note the `NAME` of the instance


Create a database within the instance

```
gcloud spanner databases create library --instance={name}
```
> Replace the occurrence of `{name}` above with instance name of your spanner-sandbox-instance.

> A single service instance may support multiple databases. In this case we're creating a database named `library`.

Push the app

```
cf push
```
> The supplied `manifest.yml` sets the required environment variables.

> Java Config via [java-cfenv](https://github.com/pivotal-cf/java-cfenv) takes care to auto-fetch Google Spanner instance credentials at startup which are used to setup a connection and support on-demand database transactions.

Visit the route for the app you just pushed in your favorite browser.

## What does this app do?

Basically allows a librarian to maintain a simple inventory of books.

Try the different actions available:

* listing books
* adding a new book
* searching for a book by its ID