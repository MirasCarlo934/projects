using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CS21A_2018_Midterm_Project
{
    class ReliefTester
    {
        static void Main(string[] args)
        {
            Beneficiary pangarap, virlanie, tuloy;
            ReliefCenter kapuso, kapamilya;

            kapuso = new ReliefCenter("Kapuso");
            kapamilya = new ReliefCenter("Kapamilya");

            pangarap = new Beneficiary("Pangarap Foundation", kapuso);
            virlanie = new Beneficiary("Virlanie Foundation", kapamilya);
            tuloy = new Beneficiary("Tuloy Foundation", kapuso);

            // 1 rice, 1 water, 3 tuna, 5 sardines, 6 noodles

            kapuso.ReceiveGoods("rice", 1000);
            kapuso.ReceiveGoods("water", 1000);
            kapuso.ReceiveGoods("tuna", 1000);
            kapuso.ReceiveGoods("sardines", 1000);
            kapuso.ReceiveGoods("noodles", 1000);

            kapamilya.ReceiveGoods("rice", 500);
            kapamilya.ReceiveGoods("water", 500);
            kapamilya.ReceiveGoods("tuna", 500);
            kapamilya.ReceiveGoods("corned beef", 500); //ERROR
            kapamilya.ReceiveGoods("sardines", 500);
            kapamilya.ReceiveGoods("noodles", 500);

            pangarap.RequestPacks(50); // 50 water, 50 rice, 150 tuna, 250 sardines, 300 noodles OK
            virlanie.RequestPacks(100); // 100 water, 100 rice, 300 tuna, 500 sardines, 600 noodles ERROR
            tuloy.RequestPacks(75); // 75 water, 75 rice, 225 tuna, 375 sardines, 450 noodles OK
            virlanie.RequestPacks(50); // 50 water, 50 rice, 150 tuna, 250 sardines, 300 noodles OK
            pangarap.RequestPacks(50); // 50 water, 50 rice, 150 sardines, 300 noodles ERROR
            tuloy.RequestPacks(40); // 40 water, 40 rice, 120 sardines, 240 noodles OK

            Console.WriteLine("---------- RELIEF GOODS REPORT ----------");
            Console.WriteLine("{0} has requested a total of {1} packs", pangarap.GetName(), pangarap.GetRequested());
            Console.WriteLine("{0} has requested a total of {1} packs", virlanie.GetName(), virlanie.GetRequested());
            Console.WriteLine("{0} has requested a total of {1} packs", tuloy.GetName(), tuloy.GetRequested());
            Console.WriteLine("----------");
            Console.WriteLine("{0} has released {1} packs", kapuso.GetName(), kapuso.GetPackCount());
            Console.WriteLine("Remaining Goods:");
            kapuso.PrintInventory();
            Console.WriteLine("----------");
            Console.WriteLine("{0} has released {0} packs", kapamilya.GetName(), kapamilya.GetPackCount());
            Console.WriteLine("Remaining Goods:");
            kapamilya.PrintInventory();
            Console.WriteLine("-----------------------------------------");
        }
    }
}
