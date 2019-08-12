package us.ihmc.cd

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.gradle.api.Action
import java.io.IOException
import java.util.concurrent.TimeUnit

open class RemoteExtension
{
   fun session(address: String, username: String, password: String, action: Action<RemoteConnection>)
   {
      session(address, {sshClient -> sshClient.authPublickey(username, password)} , action)
   }

   fun session(address: String, username: String, action: Action<RemoteConnection>)
   {
      session(address, {sshClient -> sshClient.authPublickey(username)} , action)
   }

   class RemoteConnection(val ssh: SSHClient, val sftp: SFTPClient)
   {
      fun exec(command: String, timeout: Double = 5.0)
      {
         var session: Session? = null
         try
         {
            session = ssh.startSession()

            LogTools.quiet("Executing on ${ssh.remoteHostname}: \"$command\"")
            val command = session.exec(command)
            LogTools.quiet(IOUtils.readFully(command.inputStream).toString())
            command.join((timeout * 1e9).toLong(), TimeUnit.NANOSECONDS)
            LogTools.quiet("** exit status: " + command.exitStatus)
         }
         finally
         {
            try
            {
               if (session != null)
               {
                  session.close();
               }
            }
            catch (e: IOException)
            {
               // do nothing
            }
         }
      }
   }

   private fun session(address: String, authenticate: (SSHClient) -> Unit, action: Action<RemoteConnection>)
   {
      val sshClient = SSHClient()
      sshClient.loadKnownHosts()
      sshClient.addHostKeyVerifier(PromiscuousVerifier()) // TODO: Try removing this again
      sshClient.connect(address)

      try
      {
         authenticate(sshClient)

         val sftpClient: SFTPClient = sshClient.newSFTPClient()
         try
         {
            action.execute(RemoteConnection(sshClient, sftpClient))
         }
         finally
         {
            sftpClient.close()
         }
      }
      finally
      {
         sshClient.disconnect()
      }
   }
}