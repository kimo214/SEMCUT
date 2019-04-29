/**************************************************************************************************************/
#include <stdio.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#define F_CPU 16000000UL      // Configure System Clock = DCO = 16Mhz
#define periodTime 0.004096
volatile double Overflows,BtOverflows;
volatile double Value; 
volatile double t;
volatile int velocity;
volatile int freq;


volatile int state = 1;
volatile int print1 = 0;
volatile int rpm =0;
volatile int overflows =0;
volatile int overflowsDet =0;
volatile int TimerVal =0;
volatile int calc =0;

volatile int ok =0;




//PB0 --> 8

// F = 16000000 , 2^16 = 65536 
// 16000000/1=16000000
//feq cpu/(prescaler*top) --> freq timer

/* Overflow interrupt vector */
ISR(TIMER1_OVF_vect){                 // here if no input pulse detected 
  Overflows++;
  
  //velocity = 5 --> time = 1.221451224
  if (Overflows>298) velocity=0;
  
  BtOverflows++;
  if(BtOverflows>122){
    print1=1;
    BtOverflows=0;
    
  }
}


//SPEED

/* ICR interrupt vector */
ISR(TIMER1_CAPT_vect){
  Value = ICR1;
  t=(Overflows*periodTime)+(Value/16000000);
  freq=60/t;
  velocity=((2*3.14159265*0.)/t)*(3600/1000);
  TCNT1 = 0;
  Overflows=0;
}


//RPM

void init_Timer0(void){
  EICRA |= 1 << ISC00| 1 << ISC01;  //triggers rising edge
  EIMSK |= 1 << INT0;
  TIMSK0 |=  1 << TOIE0;
  TCCR0B |= 1 << CS00 | 1 << CS02; // prescaler = 1024
}

ISR(TIMER0_OVF_vect){
  overflows++;
}

ISR(INT0_vect){
  if(state == 1){
  TCNT0 = 0;
  overflows =0;
  state =0;
  }
  else{
    calc = 1;
  state=1;
  TimerVal = TCNT0; 
  overflowsDet=overflows;
  }
}


int main(void)
{
  //====DDR
  Serial.begin(9600);
  Overflows=0;
  BtOverflows=0; 
  TCCR1B |= 1 << ICNC1;
  TCCR1B |= 1<<ICES1;
  TIMSK1 |= 1<<ICIE1;
  TCCR1B |= 0b00000001; //prescaler = 1
  TIMSK1 |= 1<<TOIE1;
 
  init_Timer0();

  char btdata[10];
  btdata[0]='$';
  btdata[1]='!';
  int i;
  int k;
  int count=2;
  int temp=0;
  sei();
while (1) 
  {
      if(print1 == 1){
        if(calc == 1){
      rpm =  (937500/(TimerVal + (overflowsDet * 255)));
      calc = 0;
        }
      temp=rpm;
      i=3;
      while(true){
        btdata[count]=(temp/(int)(pow(10,i)))+'0';
        temp=(temp%(int)(pow(10,i)));
        count++;
        i--;
        if(i<0)break;
      }
      i=3;
      temp=velocity;
      while(true){
        btdata[count]=(temp/(int)(pow(10,i)))+'0';
        temp=(temp%(int)(pow(10,i)));
        count++;
        i--;
        if(i<0)break;
      }
      count=2;
      Serial.print(btdata);
      //Serial.println(btdata);
      print1=0;
    }
  }

}   
