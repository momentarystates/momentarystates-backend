# dgdg-backend


## Development

#### define `local.conf` and `test.local.conf`

If you want to run the project locally, you need to add a file called `local.conf` to your `/conf` directory, where 
you can define specific configuration parameters that differ from the general configuration `appliation.conf` and 
`development.conf`. This file is in the list of ignored files, so is not checked in and parameters that are specified
will remain on your local machine.

This is usually necessary for your local database configuration. Please see `application.conf` for parameters that can 
be overridden.

#### define port for backend

Create a file `.sbtopts` with the following content to specify the port when 
running in development mode. This file is ignored by git. 

```
-Dhttp.port=9070
```

#### Notes

**embedded postgresql for unit tests**

In order to perform unit tests, we use an embedded postgresql. It is not actively 
maintained anymore and some transitive dependencies need to be adjusted. Please see 
here for Details: https://github.com/yandex-qatools/postgresql-embedded/issues/153

