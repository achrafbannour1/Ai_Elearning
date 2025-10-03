import { AfterViewInit, Component, ElementRef, QueryList, ViewChildren } from '@angular/core';
import { CommonModule } from '@angular/common';

type Course = {
  title: string;
  teacher: string;
  rating: number;
  price: number;
  classes: number;
  students: number;
  image: string;
  bestSeller?: boolean;
};

@Component({
  selector: 'app-popular-courses',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './popular-courses.component.html',
  styleUrls: ['./popular-courses.component.css']
})
export class PopularCoursesComponent implements AfterViewInit {
  @ViewChildren('card') cards!: QueryList<ElementRef<HTMLElement>>;

  // Expose Math pour l'utiliser dans le template
  readonly Math = Math;

  courses: Course[] = [
    {
      title: 'Full stack modern javascript',
      teacher: 'Colt stelle',
      rating: 4.4,
      price: 20,
      classes: 12,
      students: 150,
      image: 'assets/courses/course-1.png',
      bestSeller: true
    },
    {
      title: 'Design system with React programme',
      teacher: 'Colt stelle',
      rating: 4.5,
      price: 20,
      classes: 12,
      students: 130,
      image: 'assets/courses/course-2.png',
      bestSeller: true
    },
    {
      title: 'Design banner with Figma',
      teacher: 'Colt stelle',
      rating: 5,
      price: 20,
      classes: 12,
      students: 120,
      image: 'assets/courses/course-3.png',
      bestSeller: true
    }
  ];

  ngAfterViewInit(): void {
    const io = new IntersectionObserver((entries) => {
      entries.forEach(e => {
        if (e.isIntersecting) {
          e.target.classList.add('in');
          io.unobserve(e.target);
        }
      });
    }, { threshold: 0.2 });

    this.cards.forEach(c => io.observe(c.nativeElement));
  }
}
