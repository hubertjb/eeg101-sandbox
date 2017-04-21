%% Compare raw and filtered

raw = csvread('/home/hubert/Documents/eeg-101-sandbox/data/HighPass_FullSampled_Raw_EEG1.csv',2,1);
hpfilt = csvread('/home/hubert/Documents/eeg-101-sandbox/data/HighPass_FullSampled_Filtered_EEG1.csv',2,1);

%% Define filt

fs = 256;
fc = 1;

% activeFilter = new Filter(filterFreq, "highpass", 2, 2, 0);
[b,a] = butter(2,fc/(fs/2),'high');
hpfilt2 = filter(b,a,raw,[],1);

% App coefficients (1 Hz cutoff)
bApp = [0.9827947082978767, -1.9655894165957535, 0.9827947082978767];
aApp = [1.0, -1.96529337262269, 0.9658854605688172];
hpfilt3 = filter(bApp,aApp,raw,[],1);

% App coefficients (2 Hz cutoff)
bApp = [0.9658852897440701, -1.9317705794881401, 0.9658852897440701];
aApp = [1.0, -1.9306064272196681, 0.9329347317566122];
hpfilt4 = filter(bApp,aApp,raw,[],1);

%% Plot

ch = 1;

figure
hold on
plot(raw(:,ch) - mean(raw(:,ch)))
plot(hpfilt(:,ch))
plot(hpfilt2(:,ch))
plot(hpfilt3(:,ch))
plot(hpfilt4(:,ch))
legend('Raw','Filt - app','Filt -MATLAB', 'Filt in MATLAB with app coefficients (1 Hz)', ...
       'Filt in MATLAB with app coefficients (2 Hz)')

% TODO: Make sure the coefficients are not rounded when imported from Java!


%% Analyze frequency response

[appH,appF] = freqz(bApp,aApp,2048,fs);
[H,F] = freqz(b,a,2048,fs);

ax = [];

figure
ax(1) = subplot(2,1,1);
hold on
plot(appF,20*log10(abs(appH)))
plot(F,20*log10(abs(H)))
xlabel('Frequency (Hz)')
ylabel('Magnitude (dB)')
legend('App','MATLAB')
ax(2) = subplot(2,1,2);
hold on
plot(appF,angle(appH)/pi*180)
plot(F,angle(H)/pi*180)
xlabel('Frequency (Hz)')
ylabel('Phase (degrees)')
legend('App','MATLAB')

linkaxes(ax,'x');


%% Compare coefficients

figure
subplot(2,1,1)
hold on
stem(bApp)
stem(b)
title('b coefficients')
legend('App','MATLAB')

subplot(2,1,2)
hold on
stem(aApp)
stem(a)
title('a coefficients')
legend('App','MATLAB')

figure
hold on
plot(bApp-b)
plot(aApp-a)
legend('b','a')

%%

fs = 256;
fc = 30;

% activeFilter = new Filter(filterFreq, "highpass", 2, 2, 0);
[bLow,aLow] = butter(5,fc/(fs/2),'low');
% hpfilt2 = filter(b,a,raw,[],1);
freqz(bLow,aLow,2048,fs)
