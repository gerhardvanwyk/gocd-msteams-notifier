# gocd-slack-build-notifier (Under construction)
MsTeams based GoCD build notifier -  Used [ashwanthkumar/gocd-slack-build-notifier](https://github.com/ashwanthkumar/gocd-slack-build-notifier) as reference.

There is a plant.uml diagram with the [Design plugin.png](Plugin.png.)

* The notification plugin implements a GoPlugin.class and are placed in /plugins/external (GoCD server Classpath)
* During server startup the plugin is registerd with the server
* The server then start to send notification to the plugin depending on the plugins' configuration
* The default configuration is done via file, as below
* Configuration can also be changed via the frontend. 
* When a notification is received the plugin will fetch more information from the GoCD API (Pipeline History)
* This information is parsed depending on the configuration, (For Example. Trigger)
* The plugin the creates a message to send to MsTeams (Teams and channels are configured)
* The plugin fetch a Bearer Token from MS openID server
* The plugin then send the notification to the teams

## Setup
Build the jar & place it in /plugins/external & restart Go Server.

## Testing
Build the docker image. It will place the created jar in the correct folder. Run Docker with
```shell
-d -p8153:8153 -p8154:8154
```
It downloads and run GoCD 17.11.0

## Configuration
All configurations are in [HOCON](https://github.com/typesafehub/config) format. Plugin searches for the configuration file in the following order

1. File defined by the environment variable `GO_NOTIFY_CONF`.
2. `go_notify.conf` at the user's home directory. Typically it's the `go` user's home directory (`/var/go`).
3. `go_notify.conf` present at the `CRUISE_SERVER_DIR` environment variable location.

You can find the details on where / how to setup environment variables for GoCD at the [documentation](https://docs.gocd.org/current/installation/install/goCdClient/linux.html#location-of-gocd-goCdClient-files).

Minimalistic configuration would be something like
```hocon
gocd.msteams {

  # MsTeams API 
  api-msteams-host = "http://localhost:8153/"
  msteams = [{
    team = {id}
    display-name = "GoCD Build Bot"
    icon-url ="http://iconlib.com/brokonbuild"
    channel = ["{channelId1}", "{channelId2}", ... ]
    pipelines = [{
        name = {Regex}
        stage = {Regex}
        group = {Regex}
        statuses = [Broken, Failed...]
    }]
  }]

  # GoCD Server 
  login = "someuser"
  password = "somepassword"
  goCdClient-host = "http://localhost:8153/"
  
  # optional fields
  api-token = "a-valid-token-from-gocd-goCdClient" Not used currently
  display-console-log-links = true
  display-material-changes = true
  process-all-configuration = true
  trancate-change = true
  proxy {
    hostname = "localhost"
    port = "5555"
    type = "socks" # acceptable values are http / socks
  }
}
```
- `api-msteams-host` - The host URL of the MsTeams API
- `MS Teams` - List. Setup per Team on the MsTeams application
  - `team` - Team's id
  - `display-name` - Display name on top of the notification
  - `icon-url` - Icon displayed with the notification
  - `channel` - List of channel Ids 
  - `pipelines` - List of Pipeline configuration objects 
    - `group` - Regular expression for group names
    - `name` - Regular expression for pipeline names
    - `stage` - Regular expression for stage names
    - `statuses` - List of pipeline status to display the notification for. Valid values are passed, failed, cancelled, building, fixed, broken or all.
- `login` - Login for a Go user who is authorized to access the REST API.
- `password` - Password for the user specified above. You might want to create a less privileged user for this plugin.
- `*api-token` - (ignore)Valid GoCD access token. Available starting from v19.2.0 (https://api.gocd.org/current/#bearer-token-authentication). If both login/password and api-token are present, api-token takes precedence.
- `goCdClient-host` - FQDN of the Go Server. All links on the slack channel will be relative to this host.

Optional
- `display-console-log-links` - Display console log links in the notification. Defaults to true, set to false if you want to hide.
- `displayMaterialChanges` - Display material changes in the notification (git revisions for example). Defaults to true, set to false if you want to hide.
- `process-all-configuration` - If true, all matching configuration are applied instead of just the first.
- `truncate-changes` - If true, displays only the latest 5 changes for all the materials. (Default: true)
- `proxy` - Specify proxy related settings for the plugin.
  - `proxy.hostname` - Proxy Host
  - `proxy.port` - Proxy Port
  - `proxy.type` - `socks` or `http` are the only accepted values.
  
## Configuring the plugin for GoCD on Kubernetes using Helm

### Creating a Kubernetes secret to store the config file

- Create a file that has the config values, for example `go_notify.conf`
- Then create a Kubernetes secret using this file in the proper namespace 

```
kubectl create secret generic slack-config \
--from-file=go_notify.conf=go_notify.conf \
--namespace=gocd
```


### Adding the plugin
- In order to add this plugin, you have to use a local values.yaml file that will override the default [values.yaml](https://github.com/helm/charts/blob/master/stable/gocd/values.yaml) present in the official GoCD helm chart repo.
- Add the .jar file link from the releases section to the `env.extraEnvVars` section as a new environment variable.
- The environment variable name must have `GOCD_PLUGIN_INSTALL` prefixed to it.
- Example

```
env:
  extraEnvVars:
    - name: GOCD_PLUGIN_INSTALL_slack-notification-plugin
      value: https://github.com/ashwanthkumar/gocd-slack-build-notifier/releases/download/v1.3.1/gocd-slack-notifier-1.3.1.jar
    - name: GO_NOTIFY_CONF
      value: /tmp/slack/go_notify.conf
```
- Make sure to add the link of the release you want to use.
- If you want to specify a custom path for the `go_notify.conf` file you can use the `GO_NOTIFY_CONF` environment variable as given above.


### Mounting the config file

- Mount the previously secret to a path by adding the following configuration to the local values.yaml

```
persistence:
  extraVolumes:
    - name: slack-config
      secret:
        secretName: slack-config
        defaultMode: 0744

  extraVolumeMounts:
    - name: slack-config
      mountPath: /tmp/slack
      readOnly: true
```
- If you want to use a custom config location by specifying `GO_NOTIFY_CONF`, then you can use the above `mountPath`. If not, change the `mountPath` to `/var/go` as it is the default `go` user's home directory.
- Then applying the local values.yaml that has these values added to it will result in a new Go Server pod being created that has the plugin installed and running.

## License
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
