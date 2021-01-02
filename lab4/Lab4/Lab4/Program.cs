using Lab4.models;
using System;
using System.Collections.Generic;

namespace Lab4
{

    class Program
    {
        static void Main(string[] args)
        {
            List<string> HOSTS_OR_FILES_TO_DOWNLOAD = new List<string> {
                "www.cs.ubbcluj.ro/~rlupsa/edu/pdp",
                "facebook.com",
                "google.com",
            };
            DirectlyImplementedCallbacks.execute(HOSTS_OR_FILES_TO_DOWNLOAD);
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");

            ImplementationUsingTasks.execute(HOSTS_OR_FILES_TO_DOWNLOAD);
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");

            TaskWithAwait.execute(HOSTS_OR_FILES_TO_DOWNLOAD);
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");
            Console.WriteLine("\n\n\n==============================================================================================\n\n\n");


        }

    }

}
