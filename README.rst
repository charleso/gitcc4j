=======
gitcc4j
=======

This is yet-another Git/Clearcase bridge, this time written in Java.
Essentially this is port of the Python version I originally wrote last year.
I would recommend most people requiring this functionality look to that
incarnation first as it has more exposure and is likely to be less buggy. In
additional the Java components for Clearcase have broken, at least in my opinion
to be slower than the native ones. However, in comparison to Git it all seems
rather slow really.

The reason for this clone is that I switched to Ubuntu at work recently and
could not longer call cleartool commands, as Clearcase requires a specific and
older version of the kernel. As an alternative Rational offer Clearcase Remote
Client (CCRC) which is a Java based Clearcase alternative. Essentially this is
just an Java/Eclipse front-end to a bunch of webservices backed by a *real*
Clearcase server.

One of the things I've been able to achieve in this version that I have
previously craved is the ability to run a 'daemon' which continually syncs Git
with Clearcase in the background. What was lacking before was authenticating as
any user to ensure that ownership of commits isn't lost.

Death to Clearcase!

Config
======

Pretty much the same as the python version, with a few extra annoying UCM ones
which I can't seem to avoid.

::

 [core]
 type = UCM
 include = folderA|folderB/folderC

 # This is a new feature for UCM to allow gitcc to handle renames across
 # components as well as .gitignore files in the root folder
 ignore.level = 1

 # These are for CCRC
 url = http://clearcasewebserver:12080/
 username = charleso 
 password = password
 group = somegroup

 # These are for the daemon to email when something goes wrong
 email.recipients = foo@example.com|bar@example.com
 email.sender = charleso@charleso.org
 email.smtp = smpt.example.com

 [master]
 clearcase = /views/test/prod
 branches = branch_a|branch_b
 # These are new - there must be an easy way to work them out via the API
 integration = /views/test_int/prod
 stream = dbid:110825@replicauuid:a0bc60b03c1011dd94080002cea84b4c

Users
=====

This goes in the .git/users files. You don't really need the extra view
information. This is just a requirement at my work.

::

 Charles O'Farrell|charleso@charles.org|password|/views/charleso
 Joe Smith|joe@smith.com|password|/views/joes
