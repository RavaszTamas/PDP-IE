using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Lab4.models
{
    class DirectlyImplementedCallbacks
    {
        private Socket _connection;
        private string _hostname;
        private string _endpoint;
        private IPEndPoint _remoteEndpoint;
        int _id;
        public const int BUFFER_SIZE = 2048;
        public byte[] buffer;
        StringBuilder respone;
        private DirectlyImplementedCallbacks(Socket connection,string hostname,string endpoint,IPEndPoint remoteEndpoint, int id)
        {
            _connection = connection;
            _hostname = hostname;
            _endpoint = endpoint;
            _remoteEndpoint = remoteEndpoint;
            _id = id;
            buffer = new byte[BUFFER_SIZE];
            respone = new StringBuilder();
        }

        public static void execute(List<string> entriesToDownload)
        {
            int id = 1;
            foreach (string item in entriesToDownload)
            {
                startTheDownload(item, id);
                id++;
            }
            Thread.Sleep(2000);
        }

        private static void startTheDownload(string host, int id)
        {
            var ipHostInfo = Dns.GetHostEntry(host.Split('/')[0]);
            var ipAddress = ipHostInfo.AddressList[0];
            var remoteEndpoint = new IPEndPoint(ipAddress, HttpUtils.HTTP_PORT);

            var hostname = host.Split('/')[0];
            var endPoint = host.Contains("/") ? host.Substring(host.IndexOf("/")) : "/";


            var socket = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            DirectlyImplementedCallbacks session = new DirectlyImplementedCallbacks(socket, hostname, endPoint, remoteEndpoint, id);

            session.Start();

        }

        private void Start()
        {
            _connection.BeginConnect(_remoteEndpoint, Connected, null);
        }

        private void Connected(IAsyncResult ar)
        {
            _connection.EndConnect(ar);
            Console.WriteLine("direct thread: {0} --> Socket connected to {1} ({2})", _id, _hostname, _connection.RemoteEndPoint);

            var byteData = Encoding.ASCII.GetBytes(HttpUtils.getRequestString(_hostname, _endpoint));

            _connection.BeginSend(byteData, 0, byteData.Length, 0, Sent, null);
             
        }

        private void Sent(IAsyncResult ar)
        {
            var bytesSent = _connection.EndSend(ar);
            Console.WriteLine("direct thread: {0} --> {1} have been sent to the server bytes to server.", _id, bytesSent);
            _connection.BeginReceive(buffer, 0, BUFFER_SIZE, 0, Receiving, null);
        }

        private void Receiving(IAsyncResult ar)
        {
            try
            {
                var bytesRead = _connection.EndReceive(ar);
                respone.Append(Encoding.ASCII.GetString(buffer, 0, bytesRead));

                if (!HttpUtils.responseHeaderFullyObtained(respone.ToString()))
                {
                    _connection.BeginReceive(buffer, 0, buffer.Length, 0, Receiving, null);
                }
                else
                {
                    var responseBody = HttpUtils.getResponseBody(respone.ToString());

                    var contentLengthHeaderValue = HttpUtils.getContentLength(respone.ToString());
                    if (responseBody.Length < contentLengthHeaderValue)
                    {
                        _connection.BeginReceive(buffer, 0, BUFFER_SIZE, 0, Receiving, null);
                    }
                    else
                    {
                        foreach (var i in respone.ToString().Split('\r', '\n'))
                            Console.WriteLine(i);
                        Console.WriteLine(
                            "direct thread: {0} --> Response received from server: expected {1} chars in body, got {2} chars (headers + body in total)",
                            _id, contentLengthHeaderValue, respone.Length);

                        // release the socket
                        _connection.Shutdown(SocketShutdown.Both);
                        _connection.Close();
                    }

                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }
    }
}
