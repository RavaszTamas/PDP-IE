using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Lab4.models
{
    class TaskWithAwait
    {
        private Socket _connection;
        private string _hostname;
        private string _endpoint;
        private IPEndPoint _remoteEndpoint;
        int _id;
        public const int BUFFER_SIZE = 2048;
        public byte[] buffer;
        StringBuilder respone;
        private TaskWithAwait(Socket connection, string hostname, string endpoint, IPEndPoint remoteEndpoint, int id)
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

            List<Task> tasks = new List<Task>();

            foreach (string item in entriesToDownload)
            {
                tasks.Add(Task.Factory.StartNew(() => startTheDownload(item, id)));
                Thread.Sleep(10);
                id++;
            }

            Task.WaitAll(tasks.ToArray());
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
            TaskWithAwait session = new TaskWithAwait(socket, hostname, endPoint, remoteEndpoint, id);

            session.Start().Wait();

        }

        private Task Connect()
        {
            return Task.Factory.FromAsync(_connection.BeginConnect, _connection.EndConnect, _remoteEndpoint, null);
            /*
            TaskCompletionSource<bool> promise = new TaskCompletionSource<bool>();

            _connection.BeginConnect(_remoteEndpoint, (IAsyncResult ar) => {
                _connection.EndConnect(ar); promise.SetResult(true);
            }, null);
            return promise.Task;
            */
        }

        private Task<int> Send(Socket conn, byte[] buf, int index, int count)
        {
            TaskCompletionSource<int> promise = new TaskCompletionSource<int>();
            conn.BeginSend(buf, index, count, SocketFlags.None,
                (IAsyncResult ar) => promise.SetResult(conn.EndSend(ar)),
                null);
            return promise.Task;
        }



        private Task<int> Receive(Socket conn, byte[] buf, int index, int count)
        {
            TaskCompletionSource<int> promise = new TaskCompletionSource<int>();
            conn.BeginReceive(buf, index, count, SocketFlags.None,
                (IAsyncResult ar) => promise.SetResult(conn.EndReceive(ar)),
                null);
            return promise.Task;
        }

        private async Task Start()
        {
            await Connect();

            Console.WriteLine("await thread: {0} --> Socket connected to {1} ({2})", _id, _hostname, _connection.RemoteEndPoint);

            var byteData = Encoding.ASCII.GetBytes(HttpUtils.getRequestString(_hostname, _endpoint));

            //_connection.BeginSend(byteData, 0, byteData.Length, 0, Sent, null);

            int bytesSent = await Send(_connection, byteData, 0, byteData.Length);

            Console.WriteLine("await thread: {0} --> {1} have been sent to the server bytes to server.", _id, bytesSent);
            //_connection.BeginReceive(buffer, 0, BUFFER_SIZE, 0, Receiving, null);
            int bytesRead = await Receive(_connection, buffer, 0, BUFFER_SIZE);

            await Receiving(bytesRead);
        }

        private async Task Receiving(int bytesRead)
        {
            try
            {
                respone.Append(Encoding.ASCII.GetString(buffer, 0, bytesRead));

                if (!HttpUtils.responseHeaderFullyObtained(respone.ToString()))
                {
                    //_connection.BeginReceive(buffer, 0, buffer.Length, 0, Receiving, null);
                    int bytesReadOther = await Receive(_connection, buffer, 0, BUFFER_SIZE);
                    await Receiving(bytesReadOther);
                }
                else
                {
                    var responseBody = HttpUtils.getResponseBody(respone.ToString());

                    var contentLengthHeaderValue = HttpUtils.getContentLength(respone.ToString());
                    if (responseBody.Length < contentLengthHeaderValue)
                    {
                        //_connection.BeginReceive(buffer, 0, BUFFER_SIZE, 0, Receiving, null);
                        int bytesReadOther = await Receive(_connection, buffer, 0, BUFFER_SIZE);
                        await Receiving(bytesReadOther);
                    }
                    else
                    {
                        foreach (var i in respone.ToString().Split('\r', '\n'))
                            Console.WriteLine(i);
                        Console.WriteLine(
                            "await thread: {0} --> Response received from server: expected {1} chars in body, got {2} chars (headers + body in total)",
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
