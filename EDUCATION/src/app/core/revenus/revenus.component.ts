import { Component, OnInit } from '@angular/core';
import { ChartData, ChartDataset, ChartOptions, ChartType, ChartTypeRegistry } from 'chart.js';
import { RevenusService } from 'src/services/revenus.service';

@Component({
  selector: 'app-revenus',
  templateUrl: './revenus.component.html',
  styleUrls: ['./revenus.component.css']
})
export class RevenusComponent implements OnInit {
  public lineChartData: ChartData<'line', number[]> = {
    labels: [],
    datasets: []
  };
  
  public lineChartOptions: ChartOptions<'line'> = { responsive: true };

  public lineChartType: 'line' = 'line';


  constructor(private revenusService: RevenusService) { }

  ngOnInit(): void {
    this.revenusService.getRevenus().subscribe(data => {
      this.lineChartData = {
        labels: data.dates, // ['2025-01', '2025-02', ...]
        datasets: [
          { data: data.revenus_reels, label: 'Revenus réels', borderColor: 'blue', fill: false },
          { data: data.previsions, label: 'Prévisions', borderColor: 'orange', fill: false }
        ]
      };
    });
  }

}
