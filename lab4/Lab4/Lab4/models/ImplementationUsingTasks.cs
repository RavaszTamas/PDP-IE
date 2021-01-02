using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Lab4.models
{
    class ImplementationUsingTasks
    {
        private Socket _connection;
        private string _hostname;
        private string _endpoint;
        private IPEndPoint _remoteEndpoint;
        int _id;
        public const int BUFFER_SIZE = 16;
        public byte[] buffer;
        StringBuilder respone;
        private ImplementationUsingTasks(Socket connection, string hostname, string endpoint, IPEndPoint remoteEndpoint, int id)
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
                tasks.Add(Task.Factory.StartNew(()=>startTheDownload(item, id)));
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
            ImplementationUsingTasks session = new ImplementationUsingTasks(socket, hostname, endPoint, remoteEndpoint, id);

            session.Start();

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

        private void Start()
        {
            Task promise = Connect();
            promise.ContinueWith(Connected);
            //_connection.BeginConnect(_remoteEndpoint, Connected, null);

        }


        private void Connected( Task future)
        {
            future.Wait();
            

            Console.WriteLine("task thread: {0} --> Socket connected to {1} ({2})", _id, _hostname, _connection.RemoteEndPoint);

            var byteData = Encoding.ASCII.GetBytes(HttpUtils.getRequestString(_hostname, _endpoint));

            //_connection.BeginSend(byteData, 0, byteData.Length, 0, Sent, null);
            Task<int> promise = Send(_connection, byteData, 0, byteData.Length);
            promise.ContinueWith(Sent);
        }

        private void Sent(Task<int> future)
        {
            var bytesSent = future.Result;//_connection.EndSend(ar);
            Console.WriteLine("task thread: {0} --> {1} have been sent to the server bytes to server.", _id, bytesSent);
            //_connection.BeginReceive(buffer, 0, BUFFER_SIZE, 0, Receiving, null);
            Task<int> promise = Receive(_connection, buffer, 0, BUFFER_SIZE);
            promise.ContinueWith(Receiving);

        }

        private void Receiving(Task<int> future)
        {
            try
            {
                var bytesRead = future.Result;//_connection.EndReceive(ar);
                respone.Append(Encoding.ASCII.GetString(buffer, 0, bytesRead));

                if (!HttpUtils.responseHeaderFullyObtained(respone.ToString()))
                {
                    //_connection.BeginReceive(buffer, 0, buffer.Length, 0, Receiving, null);
                    Task<int> promise = Receive(_connection, buffer, 0, BUFFER_SIZE);
                    promise.ContinueWith(Receiving);
                }
                else
                {
                    var responseBody = HttpUtils.getResponseBody(respone.ToString());

                    var contentLengthHeaderValue = HttpUtils.getContentLength(respone.ToString());
                    if (responseBody.Length < contentLengthHeaderValue)
                    {
                        //_connection.BeginReceive(buffer, 0, BUFFER_SIZE, 0, Receiving, null);
                        Task<int> promise = Receive(_connection, buffer, 0, BUFFER_SIZE);
                        promise.ContinueWith(Receiving);
                    }
                    else
                    {
                        foreach (var i in respone.ToString().Split('\r', '\n'))
                            Console.WriteLine(i);
                        Console.WriteLine(
                            "task thread: {0} --> Response received from server: expected {1} chars in body, got {2} chars (headers + body in total)",
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
